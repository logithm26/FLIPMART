@echo off
title FlipMart - Web Store & REST Server (localhost:8080)
echo ==============================================================
echo  Launching FlipMart Web Store & REST Server on localhost:8080
echo ==============================================================
if not exist "bin" mkdir bin
javac -d bin -sourcepath src src\com\shoppingcart\web\WebServer.java
echo Opening http://localhost:8080 in your default browser...
start http://localhost:8080
java -cp bin com.shoppingcart.web.WebServer
pause
