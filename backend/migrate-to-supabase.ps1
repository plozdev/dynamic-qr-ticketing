# Copies the existing Docker PostgreSQL public schema, data, and Flyway history.
# Run with the local backend stopped. The destination public schema must be empty.
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$poolerHost = 'aws-0-ap-northeast-1.pooler.supabase.com'
$poolerUser = 'postgres.ewsxgjtyxejovzddbnnd'
$databaseName = 'postgres'
$sourceContainer = 'ticketing-postgres'
$dumpInContainer = '/tmp/cyberpass-supabase-transfer.dump'
$backupDirectory = Join-Path $PSScriptRoot '.local'
$backupStamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$dumpName = "cyberpass-supabase-$backupStamp.dump"
$dumpPath = Join-Path $backupDirectory $dumpName
$listName = "cyberpass-supabase-$backupStamp.list"
$listPath = Join-Path $backupDirectory $listName
$previousPassword = [Environment]::GetEnvironmentVariable('PGPASSWORD', 'Process')
$previousSslMode = [Environment]::GetEnvironmentVariable('PGSSLMODE', 'Process')

function Invoke-Docker {
    & docker @args
    if ($LASTEXITCODE -ne 0) {
        throw "Docker command failed (exit $LASTEXITCODE)."
    }
}

try {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'Docker Desktop is required for pg_dump and pg_restore.'
    }
    & docker info --format '{{.ServerVersion}}' 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'Start Docker Desktop first.'
    }
    $containerStatus = & docker inspect --format '{{.State.Running}}' $sourceContainer 2>$null
    if ($LASTEXITCODE -ne 0 -or $containerStatus -ne 'true') {
        throw "Start $sourceContainer with: docker compose up -d postgres"
    }

    Write-Host 'Source: Docker ticketing_db. Destination: Supabase cyber_pass / public.'
    Write-Host 'Stop the local backend before continuing so no writes happen during the dump.'
    $confirmation = Read-Host 'Type MIGRATE to continue'
    if ($confirmation -cne 'MIGRATE') {
        throw 'Migration cancelled.'
    }

    $securePassword = Read-Host 'Supabase database password' -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    if ([string]::IsNullOrWhiteSpace($env:PGPASSWORD)) {
        throw 'Database password is required.'
    }
    $env:PGSSLMODE = 'require'

    # Supabase owns the public schema already. Refuse to overwrite an existing app DB.
    $tableCount = & docker run --rm --env PGPASSWORD --env PGSSLMODE postgres:16-alpine `
        psql -h $poolerHost -p 5432 -U $poolerUser -d $databaseName `
        -Atqc "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'"
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not connect to Supabase. Check the password and Session Pooler status.'
    }
    if (($tableCount | Select-Object -Last 1).Trim() -ne '0') {
        throw 'Supabase public already has tables. This script only imports into an empty public schema.'
    }

    New-Item -ItemType Directory -Path $backupDirectory -Force | Out-Null
    Write-Host 'Creating a consistent PostgreSQL dump in the Docker container...'
    Invoke-Docker exec $sourceContainer pg_dump -U ticketing_user -d ticketing_db `
        --schema=public --format=custom --no-owner --no-privileges `
        --file=$dumpInContainer
    try {
        Invoke-Docker cp "${sourceContainer}:${dumpInContainer}" $dumpPath
    }
    finally {
        & docker exec $sourceContainer rm -f $dumpInContainer | Out-Null
    }

    # The archive includes CREATE SCHEMA public; Supabase has created that schema.
    $mount = "type=bind,source=$backupDirectory,target=/backup"
    $toc = & docker run --rm --mount $mount postgres:16-alpine `
        pg_restore --list "/backup/$dumpName"
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not read the PostgreSQL dump.'
    }
    $filteredToc = @($toc | Where-Object {
        $_ -notmatch '^\d+;\s+\d+\s+\d+\s+SCHEMA\s+-\s+public(?:\s|$)'
    })
    if ($filteredToc.Count -eq 0) {
        throw 'The PostgreSQL dump has no restorable objects.'
    }
    [System.IO.File]::WriteAllLines($listPath, [string[]]$filteredToc,
        [System.Text.UTF8Encoding]::new($false))

    Write-Host 'Restoring schema, data and flyway_schema_history to Supabase...'
    Invoke-Docker run --rm --env PGPASSWORD --env PGSSLMODE --mount $mount postgres:16-alpine `
        pg_restore -h $poolerHost -p 5432 -U $poolerUser -d $databaseName `
        --no-owner --no-privileges --single-transaction --exit-on-error `
        --use-list "/backup/$listName" "/backup/$dumpName"

    $result = & docker run --rm --env PGPASSWORD --env PGSSLMODE postgres:16-alpine `
        psql -h $poolerHost -p 5432 -U $poolerUser -d $databaseName `
        -Atqc 'SELECT (SELECT count(*) FROM public.users), (SELECT count(*) FROM public.events), (SELECT count(*) FROM public.tickets), (SELECT max(version) FROM public.flyway_schema_history)'
    if ($LASTEXITCODE -ne 0) {
        throw 'Restore finished, but verification query failed. Inspect Supabase SQL Editor.'
    }
    Write-Host "Import complete. users | events | tickets | Flyway version: $result"
    Write-Host "Local backup retained at: $dumpPath"
    Write-Host 'Now start the backend with .\run-supabase.ps1.'
}
finally {
    if ($null -eq $previousPassword) {
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    }
    else {
        $env:PGPASSWORD = $previousPassword
    }
    if ($null -eq $previousSslMode) {
        Remove-Item Env:PGSSLMODE -ErrorAction SilentlyContinue
    }
    else {
        $env:PGSSLMODE = $previousSslMode
    }
}
