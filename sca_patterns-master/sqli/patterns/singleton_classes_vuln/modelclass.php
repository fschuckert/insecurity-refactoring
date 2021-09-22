<?php


class ModelClass
{
    public function getQueryStr()
    {
        $id = $_GET['id'];
        $sql = "SELECT val FROM Tests WHERE id=$id";

        return $sql;
    }
}

?>