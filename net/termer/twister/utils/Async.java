package net.termer.twister.utils;

/**
 * Utility class to run events asynchronously
 * @author termer
 * @since 0.2
 */
public class Async extends Thread {
	private Runnable _TASK_ = null;
	private String _NAME_ = null;
	
	/**
	 * Sets up initial values
	 * @param task the task to run
	 * @param name the name of the task
	 * @since 0.2
	 */
	private Async(Runnable task, String name) {
		_TASK_ = task;
		_NAME_ = name;
	}
	
	public void run() {
		if(_NAME_!=null) {
			setName(_NAME_);
		}
		if(_TASK_!=null) {
			_TASK_.run();
		}
	}
	
	/**
	 * Runs the provided task asynchronously
	 * @param task The task to run
	 * @param taskName The name of the task
	 * @since 0.2
	 */
	public static void run(Runnable task, String taskName) {
		new Async(task, taskName).start();
	}
	
	/**
	 * Runs the provided task asynchronously
	 * @param task The task to run
	 * @since 0.2
	 */
	public static void run(Runnable task) {
		new Async(task, null).start();
	}
}
