<?php

class Kohana
{
    public static $instance;

    public static function include_paths()
    {
        return array_filter(glob('plugins/*'), 'is_dir');
    }


    /**
	 * Loads the controller and initializes it. Runs the pre_controller,
	 * post_controller_constructor, and post_controller events. Triggers
	 * a system.404 event when the route cannot be mapped to a controller.
	 *
	 * This method is benchmarked as controller_setup and controller_execution.
	 *
	 * @return  object  instance of controller
	 */
	public static function & instance()
	{
		if (self::$instance === NULL)
		{
			// Include the Controller file
			require Router::$controller_path;

			$class = new ReflectionClass(ucfirst(Router::$controller).'_Controller');			

			// Create a new controller instance
			$controller = $class->newInstance();


			// Load the controller method
			$method = $class->getMethod(Router::$method);

			if ($method->isProtected() or $method->isPrivate())
			{
				// Do not attempt to invoke protected methods
				throw new ReflectionException('protected controller method');
			}

			// Default arguments
			$arguments = Router::$arguments;

            // echo "Invoking method: $method controller:  arguments: $arguments";
			// Execute the controller method
			$method->invokeArgs($controller, $arguments);
		}

		return self::$instance;
	}



}


?>