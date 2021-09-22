<?php

class DAO 
{
    protected $id;

    public function setId($id)
    {
        $this->id = $id;
    }

    public function getSQL()
    {
        $id = intval($this->id);
        $sql = "SELECT * FROM user WHERE id=$id";
        return $sql;
    }
}



?>