$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "compile.ps1")

$javaFxVersion = "21.0.8"
$sdkLib = Join-Path $PSScriptRoot ".deps\javafx-sdk-$javaFxVersion\lib"
$classPath = (Join-Path $PSScriptRoot "out\classes") + ";" + (Join-Path $PSScriptRoot "out\test")
java --module-path $sdkLib --add-modules javafx.controls,javafx.media -ea -cp $classPath moonlit.LogicSmokeTest
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

python -m unittest discover -s test -p "*pixel*.py"
exit $LASTEXITCODE
