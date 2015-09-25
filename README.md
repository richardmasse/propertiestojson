# propertiestojson
Maven plugin to convert properties files to json files

Usage :
```
<plugin>
    <groupId>org.masse</groupId>
    <artifactId>propertiestojson-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>default-propertiestojson</id>
            <configuration>
                <propertiesSourcePath>${basedir}/src/main/properties</propertiesSourcePath>
                <jsonTargetPath>${basedir}/src/main/webcontent/js/locale</jsonTargetPath>
                <fileWilcard>*.properties</fileWilcard>
            </configuration>
            <goals>
                <goal>propertiestojson</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

