package ru.atomation.jbrowser.interfaces;

public interface DisplayableComponent {
	
	/**
	 * Выполнить действие когда компонент станет отображаемым // Run action when componnet connect to native peer
	 * @param action
	 */
	void onCreatePeer(Runnable action);
	
	/**
	 * Выполнить действие когда компонент уничтожится // Run action when natie peer destroying
	 * @param action
	 */
	void onDestroyPeer(Runnable action);

}
