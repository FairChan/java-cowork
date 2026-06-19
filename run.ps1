$ErrorActionPreference = "Stop"
& (Join-Path $PSScriptRoot "compile.ps1")

$javaFxVersion = "21.0.8"
$sdkLib = Join-Path $PSScriptRoot ".deps\javafx-sdk-$javaFxVersion\lib"
java --module-path $sdkLib --add-modules javafx.controls -cp (Join-Path $PSScriptRoot "out\classes") moonlit.GameApplication
exit $LASTEXITCODE
