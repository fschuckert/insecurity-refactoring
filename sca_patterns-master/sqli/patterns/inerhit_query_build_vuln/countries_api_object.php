<?php

require_once("./api_object.php");

class Countries_Api_Object extends Api_Object
{

    public function triggerVuln()
    {
        return $this->_get_country_by_name(($this->request['id']));
    }



    private function _get_country_by_name($val)
    {
        $where = "\n WHERE val=$val ";
        $where .= "ORDER by id DESC";
        $limit = "";
        
        return $this->_get_countries($where, $limit);
    }

    private function _get_countries($where = '', $limit = '')
    {

        // Fetch countries
        $this->query = "SELECT * FROM Tests $where $limit";

        $items = $this->db->query($this->query);
        
        return $items;
    }
}

?>