<?php

class Database
{
    private static $db;

    public static function connect()
    {
        self::$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
    }

    public static function getDatabase()
    {
        return self::$db;
    }

    public static function cleanValue($value)
    {
        if (function_exists('mysqli_real_escape_string')) {
            return self::$db->quote($value);

        } else {
            return addslashes($value);
        }
    }
}

?>