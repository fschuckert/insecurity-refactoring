<?php

class Api_Object
{
    protected $request = array();

    protected $db;
    protected $id;
    protected $query;

    public function init_request()
    {
        $this->db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
        $this->request = $_REQUEST;
    }
}

?>