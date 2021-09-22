<?php

abstract class DBInterface
{
    var $havedb = false;

    /**
	 * @param  $table
	 * @param  $col
	 * @param null $where
	 * @return null
	 */
    abstract function selectValue($table, $col, $where=null);
    abstract function escapeString($string);
}


class DBImplementation extends DBInterface
{
    function __construct()
    {
        $this->havedb=true;
    }

    function selectValue($table, $col, $where=null)
    {
        $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
        $sql = "SELECT $col FROM $table WHERE $where";

        return $db->query($sql);
    }

    function escapeString($string)
    {
        $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
        return $db->quote($string);
    }
}


global $db;
$db = new DBImplementation();

?>