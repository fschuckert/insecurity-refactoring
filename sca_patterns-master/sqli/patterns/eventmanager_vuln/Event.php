<?php
/**
 * Process queuing/execution class. Allows an unlimited number of callbacks
 * to be added to 'events'. Events can be run multiple times, and can also
 * process event-specific data. By default, Kohana has several system events.
 *
 * $Id: Event.php 3917 2009-01-21 03:06:22Z zombor $
 *
 * @package    Core
 * @author     Kohana Team
 * @copyright  (c) 2007 Kohana Team
 * @license    http://kohanaphp.com/license.html
 * @link       http://docs.kohanaphp.com/general/events
 */
final class Event {

	// Event callbacks
	private static $events = array();

	// Cache of events that have been run
	private static $has_run = array();

	// Data that can be processed during events
	public static $data;

	/**
	 * Add a callback to an event queue.
	 *
	 * @param   string   event name
	 * @param   array    http://php.net/callback
	 * @return  boolean
	 */
	public static function add($name, $callback)
	{
		if ( ! isset(self::$events[$name]))
		{
			// Create an empty event if it is not yet defined
			self::$events[$name] = array();
		}
		elseif (in_array($callback, self::$events[$name], TRUE))
		{
			// The event already exists
			return FALSE;
		}

		// Add the event
		self::$events[$name][] = $callback;

		return TRUE;
	}

	/**
	 * Get all callbacks for an event.
	 *
	 * @param   string  event name
	 * @return  array
	 */
	public static function get($name)
	{
		return empty(self::$events[$name]) ? array() : self::$events[$name];
	}



	/**
	 * Execute all of the callbacks attached to an event.
	 *
	 * @param   string   event name
	 * @param   array    data can be processed as Event::$data by the callbacks
	 * @return  void
	 */
	public static function run($name, & $data = NULL)
	{
		if ( ! empty(self::$events[$name]))
		{
			// So callbacks can access Event::$data
			self::$data =& $data;
			$callbacks  =  self::get($name);

			foreach ($callbacks as $callback)
			{
				call_user_func($callback);
			}

			// Do this to prevent data from getting 'stuck'
			$clear_data = '';
			self::$data =& $clear_data;

			// The event has been run!
			self::$has_run[$name] = $name;
		}
	}


} // End Event
