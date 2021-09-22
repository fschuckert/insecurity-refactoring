<?php

class Sanitize
{

	/**\
     * Replace any non-ascii character with its hex code with NO active db connection
     */
    public static function escape($value) {
        global $db;

        if ($db->havedb) {
            return $db->escapeString($value);
        }
    }


        
}

?>