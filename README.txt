NO SPRING BOOT / NO MAVEN
1. Put your MySQL password in src/DBConnection.java.
2. Copy mysql-connector-j-26.7.0.jar into lib/.
3. Open terminal in this folder.
4. javac -cp "lib\mysql-connector-j-26.7.0.jar" -d out src\*.java
5. java -cp "out;lib\mysql-connector-j-26.7.0.jar" HospitalServer
6. Open http://localhost:8080
This uses plain Java HttpServer + JDBC, with HTML/CSS/JS frontend and MySQL backend.
