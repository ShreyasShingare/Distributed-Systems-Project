# ==========================================
# Distributed Project - Port Forwarding Script
# ==========================================

Write-Host "1. Cleaning up old connections..." -ForegroundColor Yellow
# This stops any background jobs currently running to free up the ports
Get-Job | Remove-Job -Force

Write-Host "2. Establishing new connections based on architecture diagram..." -ForegroundColor Cyan

# UI Service -> 8084
Start-Job -ScriptBlock { kubectl port-forward service/ui-service 8084:80 }
Write-Host "   - UI Service maps to http://localhost:8084"

# Amenity Service -> 8081
Start-Job -ScriptBlock { kubectl port-forward service/amenity-service 8081:80 }
Write-Host "   - Amenity Service maps to Port 8081"

# Booking Service -> 8082
Start-Job -ScriptBlock { kubectl port-forward service/booking-service 8082:80 }
Write-Host "   - Booking Service maps to Port 8082"

# Notification Service -> 8083
Start-Job -ScriptBlock { kubectl port-forward service/notification-service 8083:80 }
Write-Host "   - Notification Service maps to Port 8083"

# User Service -> 8085
Start-Job -ScriptBlock { kubectl port-forward service/user-service 8085:80 }
Write-Host "   - User Service maps to Port 8085"

Write-Host "`n✅ All services are running in the background!" -ForegroundColor Green
Write-Host "👉 Open your browser: http://localhost:8084" -ForegroundColor White