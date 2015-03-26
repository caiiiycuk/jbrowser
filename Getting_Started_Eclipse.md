# Getting started with Eclipse

## Before you start
To easy start with jbrowser you need eclipse with maven support


## Creating simplest application with embedded browser

**Open eclipse, you see something like this:** <br />
![http://jbrowser.googlecode.com/files/0.png](http://jbrowser.googlecode.com/files/0.png)<br />

**Now we must create a new maven project:** <br />
![http://jbrowser.googlecode.com/files/1.png](http://jbrowser.googlecode.com/files/1.png)<br />

**Next step:** <br />
![http://jbrowser.googlecode.com/files/3.png](http://jbrowser.googlecode.com/files/3.png)<br />

**Next step:** <br />
1. Define your project group id <br />
2. Define your project artifact id <br />
3. Press finish <br />
![http://jbrowser.googlecode.com/files/4.png](http://jbrowser.googlecode.com/files/4.png)<br />

**Now edit a pom.xml of new project:**<br />
![http://jbrowser.googlecode.com/files/5.png](http://jbrowser.googlecode.com/files/5.png)<br />

First of all, setting java compiler to 1.6:
```
	<build>
		<plugins>
			<plugin>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<compilerVersion>1.6</compilerVersion>
					<encoding>UTF-8</encoding>
					<source>1.6</source>
					<target>1.6</target>
					<showDeprecation>true</showDeprecation>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

Then, adding information of jbrowser maven2 repository
```
	<repositories>
		<repository>
			<id>atomation-repository</id>
			<name>atomation maven repository</name>
			<url>http://atomation-repository.googlecode.com/svn/trunk</url>
			<releases>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
				<checksumPolicy>warn</checksumPolicy>
			</releases>
			<snapshots>
				<enabled>true</enabled>
				<updatePolicy>always</updatePolicy>
				<checksumPolicy>warn</checksumPolicy>
			</snapshots>
			<layout>default</layout>
		</repository>
	</repositories>
```

Now we add dependency of jbrowser:
```
	<dependencies>
		<dependency>
			<groupId>ru.atomation.jbrowser</groupId>
			<artifactId>jbrowser</artifactId>
			<version>1.9</version>
			<scope>compile</scope>
		</dependency>
	</dependencies>
```

And finally we add dependency of xulrunner. We have profiles for all supported platforms (win, linux, solaris, mac) <br />
```
	<profiles>
		<profile>
			<id>generic</id>
			<activation>
				<activeByDefault></activeByDefault>
			</activation>
		</profile>
		<profile>
			<id>linux</id>
			<dependencies>
				<dependency>
					<groupId>ru.atomation.native</groupId>
					<artifactId>xulrunner-linux</artifactId>
					<version>1.9</version>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>solaris</id>
			<dependencies>
				<dependency>
					<groupId>ru.atomation.native</groupId>
					<artifactId>xulrunner-solaris</artifactId>
					<version>1.9</version>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>macosx</id>
			<dependencies>
				<dependency>
					<groupId>ru.atomation.native</groupId>
					<artifactId>xulrunner-macosx</artifactId>
					<version>1.9</version>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>windows</id>
			<dependencies>
				<dependency>
					<groupId>ru.atomation.native</groupId>
					<artifactId>xulrunner-windows</artifactId>
					<version>1.9</version>
				</dependency>
			</dependencies>
		</profile>
	</profiles>
```

**Ok, now we must set our target platform, for this open project properties then select maven properties, and set active profile (for example linux):** <br />
![http://jbrowser.googlecode.com/files/6.png](http://jbrowser.googlecode.com/files/6.png)<br />
**Finally, create class of your jbrowser getting started snippet:**<br />
![http://jbrowser.googlecode.com/files/7.png](http://jbrowser.googlecode.com/files/7.png)<br />
```
/**
 * No restrictions for this source.
 *
 * Author: CA>>>
 * Site: atomation.ru
 * Mail: Sashusik_EntXXI@Mail.ru
 */
package ru.atomation.jbrowser.snippets;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserManager;

/**
 * Snippet creates JFrame window with JBrowser and opens
 * jbrowser web site
 * <hr>
 * Фрагмент кода создает окно, со встроенным браузером и открывает
 * веб сайт jbrowser
 * 
 * @author caiiiycuk
 */
public class GettingStartedSnippet {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        BrowserManager browserManager =
                new JBrowserBuilder().buildBrowserManager();

        JComponentFactory<Canvas> canvasFactory = browserManager.getComponentFactory(JBrowserCanvas.class);
        JBrowserComponent<?> browser = canvasFactory.createBrowser();
        
        frame.getContentPane().add(browser.getComponent());
        frame.setVisible(true);

        browser.setUrl("http://code.google.com/p/jbrowser/");
    }
}
```

**Now run our project and you should see project page**

**More intresting snippets can found in  http://code.google.com/p/jbrowser/wiki/Snippets_List**