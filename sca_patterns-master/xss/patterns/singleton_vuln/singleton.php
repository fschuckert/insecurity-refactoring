<?php

class Singleton{
    protected $variable = '';

    # from: https://de.wikibooks.org/wiki/Websiteentwicklung:_PHP:_Muster_Singleton
    protected static $_instance = null;
    /**
    * clone
    *
    * Kopieren der Instanz von aussen ebenfalls verbieten
    */
   protected function __clone() {}
   
    /**
     * constructor
     *
     * externe Instanzierung verbieten
     */
    protected function __construct() {}

    public static function getInstance()
    {
        if (null === self::$_instance)
        {
            self::$_instance = new self;
        }
        return self::$_instance;
    }

    public function setParam($par){
        $this->variable = $par;
    }

    public function getParam(){
        return $this->variable;
    }

}

?>