<?php

class Singleton
{
    public static $instance;

    public static function getInstance()
    {
        if(self::$instance == NULL) 
        {
            self::$instance = new Singleton();
        }
        return self::$instance;
    }

    public function getParam($name)
    {
        return $_GET[$name];
    }
 
}



?>