<?php


class ModelClass
{
    private $id;

    function __construct($id)
    {
        $this->id = $id;
    }

    public function getQueryStr()
    {
        $sql = "SELECT val FROM Tests WHERE id=" . $this->id;

        return $sql;
    }
}

?>