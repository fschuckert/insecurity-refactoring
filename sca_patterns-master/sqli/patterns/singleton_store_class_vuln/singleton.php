<?php

class Singleton
{
    public static $instance;

    public $providers = array();

    public static function getInstance()
    {
        if(self::$instance == NULL) 
        {
            self::$instance = new Singleton();
        }
        return self::$instance;
    }

    public function __get($property)
    {
        return $this->providers[$property];
    }

    public function __set($property, $value)
    {
        $this->providers[$property] = $value;
    }
}



?>