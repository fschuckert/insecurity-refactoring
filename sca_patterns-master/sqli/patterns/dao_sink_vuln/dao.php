<?php

class DAO
{

    private $table = 'Tests';
    private $where = array();
    private $qry = '';
    private $db = null;

    function __construct()
    {
        $this->db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
    }


    public function where($values)
    {
        $this->where = $values;
    }

    public function select()
    {
        $this->qry = 'SELECT * FROM ' . $this->table . ' ';
        $this->_where();

        return $this->_select();
    }

    function _where()
    {
        $this->qry .= 'WHERE ';
        foreach($this->where as $k => $v) 
        {
            $this->qry .= $k . ' = \'' . $v . '\'';
        }
    }

    function _select()
    {
        $this->qry .= ';';
        // echo($this->qry);
        return $this->db->query($this->qry);
    }
}




?>