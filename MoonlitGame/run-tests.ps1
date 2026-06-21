$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "compile.ps1")

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

$classPath = @(
    (Join-Path $sdkLib "javafx.base.jar"),
    (Join-Path $sdkLib "javafx.graphics.jar"),
    (Join-Path $sdkLib "javafx.controls.jar"),
    (Join-Path $sdkLib "javafx.media.jar"),
    (Join-Path $PSScriptRoot "out\classes"),
    (Join-Path $PSScriptRoot "out\test")
) -join ";"
$javaArgs = @(
    "-Djava.library.path=$sdkBin",
    "-ea",
    "-cp",
    $classPath,
    "moonlit.LogicSmokeTest"
)
& $javaPath @javaArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

python -m unittest discover -s test -p "*pixel*.py"
exit $LASTEXITCODE
