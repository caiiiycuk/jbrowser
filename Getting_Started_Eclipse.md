# Getting started with Eclipse

## Before you start
To easy start with jbrowser you need eclipse with maven support


## Creating simplest application with embedded swing browser

* Open eclipse and create new empty maven project
* Open pom.xml and set java compiler to 1.6:

	```xml
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
* Add jbrowser maven repository

	```xml
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
* Add jbrowser dependency

	```xml
		<dependencies>
			<dependency>
				<groupId>ru.atomation.jbrowser</groupId>
				<artifactId>jbrowser</artifactId>
				<version>1.9</version>
				<scope>compile</scope>
			</dependency>
		</dependencies>
	```
* Add xulrunner native dependency

	```xml
	<dependencies>
	    <dependency>
	      <groupId>ru.atomation.native</groupId>
	      <artifactId>xulrunner-[windows|linux|macosx|solaris (choose one)]</artifactId>
	      <version>1.9</version>
	    </dependency>
	</dependencies>
	```
* Finally, create main class of application

	```java
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
	
	        browser.setUrl("https://github.com/caiiiycuk/jbrowser/");
	    }
	}
	```

* Now run our project and you should see the project page

### Examples
You can find examples [here] (Snippets_List.md)
