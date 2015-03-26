package ru.atomation.jbrowser.snippets;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserManager;

public class FavIconSnippet {

	/**
	 * EN: How to load favIcon of opened page 
	 * RU: Как загрузить favIcon открытой страницы
	 * @param args
	 */
	public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        final JFrame frame = new JFrame();
        	
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        BrowserManager browserManager =
                new JBrowserBuilder().buildBrowserManager();

        JComponentFactory<Canvas> canvasFactory = browserManager.getComponentFactory(JBrowserCanvas.class);
        final JBrowserComponent<?> browser = canvasFactory.createBrowser();

        frame.getContentPane().add(browser.getComponent(), BorderLayout.CENTER);
        frame.setVisible(true);

        browser.addBrowserListener(new BrowserAdapter() {
        	@Override
        	public void onLoadingEnded() {
        		frame.setTitle("Icon url: "+ browser.getFavIcon());								
        	}
        	
        });
        
        browser.setUrl("http://ya.ru");
	}

}
