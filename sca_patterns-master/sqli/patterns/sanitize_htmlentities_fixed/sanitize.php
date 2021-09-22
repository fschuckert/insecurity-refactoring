<?php

class San
{
    public static function cleanX ($c) {
        // $val = htmlspecialchars(
        //         mysql_real_escape_string($c), 
        //         ENT_QUOTES, 
        //         "utf-8"
        //     );
        $val = htmlentities($c, ENT_QUOTES | ENT_IGNORE, "UTF-8");
        return $val;
    }
}

?>