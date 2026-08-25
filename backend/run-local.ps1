# Runs the LMS backend locally against production data.
# Port 8182 / CORS 4201 because this machine already has GlassFish on 8181
# and another Angular project on 4200.

. "$PSScriptRoot\set-local-env.ps1"
$env:SERVER_PORT = "8182"
$env:APP_CORS_ALLOWEDORIGIN = "http://localhost:4201"
& "$PSScriptRoot\mvnw.cmd" spring-boot:run
