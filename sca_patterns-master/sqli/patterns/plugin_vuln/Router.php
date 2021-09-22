<?php 

define('DOCROOT', getcwd().DIRECTORY_SEPARATOR);
define('EXT', '.php');

/**
 * Router
 *
 * $Id: Router.php 3917 2009-01-21 03:06:22Z zombor $
 *
 * @package    Core
 * @author     Kohana Team
 * @copyright  (c) 2007-2008 Kohana Team
 * @license    http://kohanaphp.com/license.html
 */
class Router {

	protected static $routes;

	public static $current_uri  = '';
	public static $query_string = '';
	public static $complete_uri = '';
	public static $routed_uri   = '';
	public static $url_suffix   = '';

	public static $segments;
	public static $rsegments;

	public static $controller;
	public static $controller_path;

	public static $method    = 'index';
	public static $arguments = array();

	/**
	 * Router setup routine. Automatically called during Kohana setup process.
	 *
	 * @return  void
	 */
	public static function setup()
	{
		if ( ! empty($_SERVER['QUERY_STRING']))
		{
			// Set the query string to the current query string
			self::$query_string = '?'.trim($_SERVER['QUERY_STRING'], '&/');
		}

		if (self::$routes === NULL)
		{
			// Load routes
			//self::$routes = Kohana::config('routes');
			self::$routes = array('_default' => '/');
		}

		// Default route status
		$default_route = FALSE;

		if (self::$current_uri === '')
		{
			// Make sure the default route is set
			if ( ! isset(self::$routes['_default']))
				throw new Kohana_Exception('core.no_default_route');

			// Use the default route when no segments exist
			self::$current_uri = self::$routes['_default'];

			// Default route is in use
			$default_route = TRUE;
		}

		// Make sure the URL is not tainted with HTML characters
		self::$current_uri = htmlspecialchars(self::$current_uri, FALSE);

		// Remove all dot-paths from the URI, they are not valid
		self::$current_uri = preg_replace('#\.[\s./]*/#', '', self::$current_uri);

		// At this point segments, rsegments, and current URI are all the same
		self::$segments = self::$rsegments = self::$current_uri = trim(self::$current_uri, '/');

		// Set the complete URI
		self::$complete_uri = self::$current_uri.self::$query_string;

		// Explode the segments by slashes
		self::$segments = ($default_route === TRUE OR self::$segments === '') ? array() : explode('/', self::$segments);

		if ($default_route === FALSE AND count(self::$routes) > 1)
		{
			// Custom routing
			self::$rsegments = self::routed_uri(self::$current_uri);
		}

		// The routed URI is now complete
		self::$routed_uri = self::$rsegments;

		// Routed segments will never be empty
		self::$rsegments = explode('/', self::$rsegments);

		// Prepare to find the controller
		$controller_path = '';
		$method_segment  = NULL;

		// Paths to search
		$paths = Kohana::include_paths();

		$pluginName = self::$rsegments[0];
		self::$method = self::$rsegments[1];
		self::$arguments['id'] = self::$rsegments[2];


		$controller_path .= $segment;

		$found = FALSE;
		foreach ($paths as $dir)
		{
			
			// Search within controllers only
			// $pluginName = end(preg_split('/\//', $dir));
			$dir .= '/controllers/';
			$dir.$controller_path = $pluginName;

			if (is_dir($dir.$controller_path) OR is_file($dir.$controller_path.EXT))
			{
				// Valid path
				$found = TRUE;
				// echo "is_dir" . '<br>';
				$c = str_replace('\\', '/', realpath($dir.$controller_path.EXT));

				if ($c = str_replace('\\', '/', realpath($dir.$controller_path.EXT)) 
					AND is_file($c))
				{
					// Set controller name
					self::$controller = $segment;
					self::$controller = $pluginName; // self added for simplicity

					// Change controller path
					self::$controller_path = $c;

					// Set the method segment
					$method_segment = $key + 1;

					break;
				}
				else {
					// echo $c . '<br>';
				}
			}
		}

		// Add another slash
		$controller_path .= '/';
	}

	/**
	 * Attempts to determine the current URI using CLI, GET, PATH_INFO, ORIG_PATH_INFO, or PHP_SELF.
	 *
	 * @return  void
	 */
	public static function find_uri()
	{
		if (isset($_SERVER['PATH_INFO']) AND $_SERVER['PATH_INFO'])
		{
			// echo "here";
			self::$current_uri = $_SERVER['PATH_INFO'];
		}
		elseif (isset($_SERVER['ORIG_PATH_INFO']) AND $_SERVER['ORIG_PATH_INFO'])
		{
			self::$current_uri = $_SERVER['ORIG_PATH_INFO'];
		}
		elseif (isset($_SERVER['PHP_SELF']) AND $_SERVER['PHP_SELF'])
		{
			self::$current_uri = $_SERVER['PHP_SELF'];
		}

		// The front controller directory and filename
		$fc = substr(realpath($_SERVER['SCRIPT_FILENAME']), strlen(DOCROOT));

		if (($strpos_fc = strpos(self::$current_uri, $fc)) !== FALSE)
		{
			// Remove the front controller from the current uri
			self::$current_uri = substr(self::$current_uri, $strpos_fc + strlen($fc));
		}

		// Remove slashes from the start and end of the URI
		self::$current_uri = trim(self::$current_uri, '/');

		if (self::$current_uri !== '')
		{
			// Reduce multiple slashes into single slashes
			self::$current_uri = preg_replace('#//+#', '/', self::$current_uri);
        }
        
		// echo self::$current_uri .'<br>';
	}

	/**
	 * Generates routed URI from given URI.
	 *
	 * @param  string  URI to convert
	 * @return string  Routed uri
	 */
	public static function routed_uri($uri)
	{
		if (self::$routes === NULL)
		{
			// Load routes
			self::$routes = Kohana::config('routes');
		}

		// Prepare variables
		$routed_uri = $uri = trim($uri, '/');

		if (isset(self::$routes[$uri]))
		{
			// Literal match, no need for regex
			$routed_uri = self::$routes[$uri];
		}
		else
		{
			// Loop through the routes and see if anything matches
			foreach (self::$routes as $key => $val)
			{
				if ($key === '_default') continue;

				// Trim slashes
				$key = trim($key, '/');
				$val = trim($val, '/');

				if (preg_match('#^'.$key.'$#u', $uri))
				{
					if (strpos($val, '$') !== FALSE)
					{
						// Use regex routing
						$routed_uri = preg_replace('#^'.$key.'$#u', $val, $uri);
					}
					else
					{
						// Standard routing
						$routed_uri = $val;
					}

					// A valid route has been found
					break;
				}
			}
		}

		if (isset(self::$routes[$routed_uri]))
		{
			// Check for double routing (without regex)
			$routed_uri = self::$routes[$routed_uri];
		}

		return trim($routed_uri, '/');
	}

} // End Router