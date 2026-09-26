# Start the Spring Boot backend against the Supabase Session Pooler.
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$previousProfile = [Environment]::GetEnvironmentVariable('SPRING_PROFILES_ACTIVE', 'Process')
$previousPassword = [Environment]::GetEnvironmentVariable('SPRING_DATASOURCE_PASSWORD', 'Process')

try {
    $securePassword = Read-Host 'Supabase database password' -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:SPRING_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    }
    finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_PASSWORD)) {
        throw 'Database password is required.'
    }

    $env:SPRING_PROFILES_ACTIVE = 'prod'
    & (Join-Path $PSScriptRoot 'gradlew.bat') bootRun
    if ($LASTEXITCODE -ne 0) {
        throw "Backend exited with code $LASTEXITCODE."
    }
}
finally {
    if ($null -eq $previousProfile) {
        Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
    }
    else {
        $env:SPRING_PROFILES_ACTIVE = $previousProfile
    }
    if ($null -eq $previousPassword) {
        Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
    }
    else {
        $env:SPRING_DATASOURCE_PASSWORD = $previousPassword
    }
}
