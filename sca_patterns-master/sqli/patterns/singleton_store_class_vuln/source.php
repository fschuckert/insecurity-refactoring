<?php

require_once('singleton.php');

function mw($class=null)
{
    $global_instance = Singleton::getInstance();
    
    return $global_instance->$class;
}

function url_param($param)
{
    return mw('url')->param($param);
}

?>