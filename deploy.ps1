# Redeploys the LMS (frontend + backend) to the production EC2 instance.
# Run this from anywhere after you've made and tested changes locally.

$ErrorActionPreference = "Stop"

$key    = "C:\Users\ituser\Desktop\Springboot projects\cranesvarsityLMS 3.0 key pair\cranesvarsity-lms-key.pem"
$server = "ubuntu@15.252.62.126"
$root   = "C:\Users\ituser\Desktop\Springboot projects\lms"

Write-Host "==> Building frontend..." -ForegroundColor Cyan
Set-Location "$root\frontend"
npx ng build

Write-Host "==> Building backend..." -ForegroundColor Cyan
Set-Location "$root\backend"
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17.0.18'
.\mvnw.cmd clean package -DskipTests -q

Write-Host "==> Uploading frontend..." -ForegroundColor Cyan
ssh -i $key $server "rm -rf /tmp/lms-frontend && mkdir -p /tmp/lms-frontend"
scp -i $key -r "$root\frontend\dist\frontend\browser\*" "${server}:/tmp/lms-frontend/"

Write-Host "==> Uploading backend jar..." -ForegroundColor Cyan
scp -i $key "$root\backend\target\template-0.0.1-SNAPSHOT.jar" "${server}:/tmp/lms-backend.jar"

Write-Host "==> Deploying on server..." -ForegroundColor Cyan
ssh -i $key $server @'
sudo rm -rf /var/www/lms/*
sudo cp -r /tmp/lms-frontend/* /var/www/lms/
sudo chown -R www-data:www-data /var/www/lms
sudo mv /tmp/lms-backend.jar /opt/lms-backend/app.jar
sudo chown ubuntu:ubuntu /opt/lms-backend/app.jar
sudo systemctl restart lms-backend
'@

Write-Host "==> Waiting for backend to come up..." -ForegroundColor Cyan
Start-Sleep -Seconds 25
ssh -i $key $server "sudo systemctl is-active lms-backend"

Write-Host "==> Done. Checking live site..." -ForegroundColor Green
curl.exe -s -o /dev/null -w "https://lms.cranesvarsity.com -> HTTP %{http_code}`n" https://lms.cranesvarsity.com
curl.exe -s -o /dev/null -w "https://lms.cranesvarsity.com/api -> HTTP %{http_code}`n" https://lms.cranesvarsity.com/api/students
