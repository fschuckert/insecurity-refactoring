<?php

class Application
{
    private static $_app;

    

    public static function app()
	{
		return self::$_app;
	}

    public static function setApplication($app)
	{
        if(self::$_app===null || $app===null)
        {
            self::$_app=$app;
        }
    }
    
    
}