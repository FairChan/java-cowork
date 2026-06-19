$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "compile.ps1")

$javaFxVersion = "21.0.8"
$sdkLib = Join-Path $PSScriptRoot ".deps\javafx-sdk-$javaFxVersion\lib"
$classPath = (Join-Path $PSScriptRoot "out\classes") + ";" + (Join-Path $PSScriptRoot "out\test")
java --module-path $sdkLib --add-modules javafx.controls -ea -cp $classPath moonlit.LogicSmokeTest
exit $LASTEXITCODE
