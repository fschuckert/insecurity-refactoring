<?php

require_once("./modelclass.php");

class Singleton
{
    public static function strong_create()
    {
        $args  = func_get_args();
        $class = array_shift($args);

        $reflector = new ReflectionClass($class);
        return $reflector->newInstanceArgs($args);
    }
}


?>