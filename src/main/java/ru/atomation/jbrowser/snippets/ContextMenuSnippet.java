package ru.atomation.jbrowser.snippets;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMNode;

import ru.atomation.jbrowser.impl.ContextMenuObject;
import ru.atomation.jbrowser.impl.DefaultBrowserClipboardManager;
import ru.atomation.jbrowser.impl.JBrowserBuilder;
import ru.atomation.jbrowser.impl.JBrowserCanvas;
import ru.atomation.jbrowser.impl.JBrowserComponent;
import ru.atomation.jbrowser.impl.JBrowserFrame;
import ru.atomation.jbrowser.impl.JComponentFactory;
import ru.atomation.jbrowser.interfaces.BrowserAdapter;
import ru.atomation.jbrowser.interfaces.BrowserManager;
import ru.atomation.jbrowser.interfaces.ContextMenuAction;
import ru.atomation.jbrowser.interfaces.ContextMenuFlags;

/**
 * EN: How to enable context menu in borwser 
 * RU: Как включить контекстное меню в браузере
 * @author caiiiycuk
 *
 */
public class ContextMenuSnippet {

	private static DefaultBrowserClipboardManager browserClipboardManager;
	private static BrowserManager browserManager;
	
	public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) (screenSize.getWidth() * 0.75f),
                (int) (screenSize.getHeight() * 0.75f));
        frame.setLocationRelativeTo(null);

        browserManager = new JBrowserBuilder().buildBrowserManager();

        JComponentFactory<Canvas> canvasFactory = browserManager.getComponentFactory(JBrowserCanvas.class);
        final JBrowserComponent<?> browser = canvasFactory.createBrowser();
        
        browser.addBrowserListener(new BrowserAdapter() {
			private DefaultContextMenu contextMenu;

			@Override
        	public void onBrowserAttached() {
        		browserClipboardManager = new DefaultBrowserClipboardManager(browser, browser.getClipboardCommands());
        		contextMenu = new DefaultContextMenu(browser);
        	}
			
			@Override
			public void showContextMenu(long aContextFlags, nsIDOMEvent aEvent,
					nsIDOMNode aNode) {
				ContextMenuObject contextMenuObject = new ContextMenuObject(browser, ContextMenuFlags.fromLong(aContextFlags), 
						browserClipboardManager, aEvent, aNode);
				
				contextMenu.onShowContextMenu(contextMenuObject, new Object[] {aContextFlags, aEvent, aNode});
			}
        });
        
        browser.getComponent().addMouseListener(new MouseAdapter() {
        	
        	
		});
        
        
        frame.getContentPane().add(browser.getComponent());
        frame.setVisible(true);
        
        browser.setUrl("http://code.google.com/p/jbrowser");
	}

	//context menu
	
	public static class DefaultContextMenu implements ContextMenuAction {

		private final JPopupMenu contextMenu;
		private final JBrowserComponent<? extends Component> browser;
		private final MouseListener contexMenuMouseListener;

		private JMenuItem openItem;
		private JMenuItem openTabItem;
		private JMenuItem copyItem;
		private JMenuItem cutItem;
		private JMenuItem pasteItem;
		private JMenuItem copyImageItem;
		private JMenuItem copyImageLocationItem;
		private JMenuItem copyLinkAddress;
		
		private ContextMenuObject currentMenuClipboard;
		private JSeparator openSeparator;
		private JMenuItem saveImageItem;
		private JSeparator imageopSeparator;
		private JSeparator linkSeparator;
		
		public DefaultContextMenu(JBrowserComponent<? extends Component> browser) {
			this.browser = browser;
			
			this.contexMenuMouseListener = new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					hideContextMenu();
				};
			};
			
			this.browser.getComponent().addMouseListener(contexMenuMouseListener);
			
			contextMenu = new JPopupMenu();
			contextMenu.setLightWeightPopupEnabled(false);
			
			openItem = new JMenuItem("Open in new window");
			openItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						JBrowserComponent<? extends JFrame> browser = browserManager.getComponentFactory(JBrowserFrame.class).createBrowser();
						browser.getComponent().setSize(640, 480);
						browser.getComponent().setLocationRelativeTo(null);
						browser.getComponent().setVisible(true);
						browser.setUrl(currentMenuClipboard.getLinkUrl());
					}
				}
			});
			
			openTabItem = new JMenuItem("Open in same window");
			openTabItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						DefaultContextMenu.this.browser.setUrl(currentMenuClipboard.getLinkUrl());
					}
				}
			});
			
			openSeparator = new JSeparator();
			
			copyItem = new JMenuItem("Copy");
			copyItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.copySelection();
					}
				}
			});
			
			cutItem = new JMenuItem("Cut");
			cutItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.cutSelection();
					}
				}
			});
			
			pasteItem = new JMenuItem("Paste");
			pasteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.paste();
					}
				}
			});
			
			imageopSeparator = new JSeparator();
			
			saveImageItem = new JMenuItem("Save image as...");
			saveImageItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.copyImageContents();
						
					    JFileChooser chooser = new JFileChooser();
					    FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Image", "png");
					    chooser.setFileFilter(filter);
					    
					    if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					        File imageFile = new File(chooser.getSelectedFile().toString()+".png");
							Image clipboardImage = getClipboardImage();
							if (clipboardImage != null && clipboardImage instanceof RenderedImage) {
								try {
									ImageIO.write((RenderedImage) clipboardImage, "PNG", imageFile);
								} catch (IOException e1) {
									e1.printStackTrace();
									//ignoring
								}
							}
					    }
					}
				}
			});
			
			copyImageItem = new JMenuItem("Copy image");
			copyImageItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.copyImageContents();
					}
				}
			});
			
			copyImageLocationItem = new JMenuItem("Copy image link");
			copyImageLocationItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.copyImageLocation();
					}
				}
			});
			
			linkSeparator = new JSeparator();
			
			copyLinkAddress = new JMenuItem("Copy link");
			copyLinkAddress.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (currentMenuClipboard != null) {
						currentMenuClipboard.copyLinkLocation();
					}
				}
			});

			contextMenu.add(openItem);
			contextMenu.add(openTabItem);
			contextMenu.add(openSeparator);
			contextMenu.add(copyItem);
			contextMenu.add(cutItem);
			contextMenu.add(pasteItem);
			contextMenu.add(imageopSeparator);
			contextMenu.add(saveImageItem);
			contextMenu.add(copyImageItem);
			contextMenu.add(linkSeparator);
			contextMenu.add(copyImageLocationItem);
			contextMenu.add(copyLinkAddress);
		}
		
		protected void hideContextMenu() {
			contextMenu.setVisible(false);
		}

		private int getRelativeX(Container container, int absoluteX) {
			if (container != null) {
				return getRelativeX(container.getParent(), absoluteX - container.getX());
			}
			
			return absoluteX;
		}
		
		private int getRelativeY(Container container, int absoluteY) {
			if (container != null) {
				return getRelativeY(container.getParent(), absoluteY - container.getY());
			}
			
			return absoluteY;
		}
		
		@Override
		public synchronized void onShowContextMenu(final ContextMenuObject contextMenuClipboard, Object[] rawData) {
			currentMenuClipboard = contextMenuClipboard;

			openItem.setVisible(contextMenuClipboard.hasLink());
			openTabItem.setVisible(contextMenuClipboard.hasLink());
			openSeparator.setVisible(contextMenuClipboard.hasLink());
			
			copyItem.setVisible(contextMenuClipboard.canCopySelection());
			cutItem.setVisible(contextMenuClipboard.canCutSelection());
			pasteItem.setVisible(contextMenuClipboard.canPaste());
			
			saveImageItem.setVisible(contextMenuClipboard.canCopyImageContents());
			copyImageItem.setVisible(saveImageItem.isVisible());
			imageopSeparator.setVisible(
					(copyItem.isVisible() || cutItem.isVisible() || pasteItem.isVisible()) 
					&& saveImageItem.isVisible());
			
			copyImageLocationItem.setVisible(contextMenuClipboard.canCopyImageLocation());
			copyLinkAddress.setVisible(contextMenuClipboard.canCopyLinkLocation());
			linkSeparator.setVisible(
					(copyItem.isVisible() || cutItem.isVisible() || pasteItem.isVisible() || saveImageItem.isVisible()) &&
					(copyImageLocationItem.isVisible() || copyLinkAddress.isVisible()));
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					contextMenu.show(browser.getComponent(), 
							getRelativeX((Container) browser.getComponent().getParent(), contextMenuClipboard.getAbsoluteX()), 
							getRelativeY((Container) browser.getComponent().getParent(), contextMenuClipboard.getAbsoluteY()));
				}
			});
		}

	}
	
    /** 
     * If an image is on the system clipboard, this method returns it;
     * otherwise it returns null.
     **/
    public static Image getClipboardImage() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    
        try {
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image image = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
                return image;
            }
        } catch (UnsupportedFlavorException e) {
        } catch (IOException e) {
        }
        
        return null;
    }
	
}
