$ErrorActionPreference = "Stop"

$mainClass = Join-Path $PSScriptRoot "out\classes\moonlit\GameApplication.class"
if (-not (Test-Path $mainClass)) {
    & (Join-Path $PSScriptRoot "compile.ps1")
}

$javaFxVersion = "21.0.8"
$sdkLib = Join-Path $PSScriptRoot ".deps\javafx-sdk-$javaFxVersion\lib"
$sdkBin = Join-Path $PSScriptRoot ".deps\javafx-sdk-$javaFxVersion\bin"
$javaCommand = Get-Command java -ErrorAction SilentlyContinue
$javaPath = if ($javaCommand) { $javaCommand.Source } else { $null }
if (-not $javaPath) {
    $jdkHome = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($jdkHome) {
        $candidate = Join-Path $jdkHome.FullName "bin\java.exe"
        if (Test-Path $candidate) {
            $javaPath = $candidate
        }
    }
}
if (-not $javaPath) {
    throw "java not found. Please install a JDK or add it to PATH."
}

$javaFxClassPath = @(
    (Join-Path $PSScriptRoot "out\classes")
) -join ";"
$javaArgs = @(
    "-Djava.library.path=$sdkBin",
    "--module-path",
    $sdkLib,
    "--add-modules",
    "javafx.controls,javafx.media",
    "-cp",
    $javaFxClassPath,
    "moonlit.GameApplication"
)
& $javaPath @javaArgs
exit $LASTEXITCODE
