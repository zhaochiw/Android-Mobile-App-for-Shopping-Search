<?php
    $count = 0;
    $keywords;
    $url = "http://svcs.eBay.com/services/search/FindingService/v1?siteid=0&SECURITY-APPNAME=Universi-ea7d-481d-9bdd-1c4a49ec3cea&OPERATION-NAME=findItemsAdvanced&SERVICE-VERSION=1.0.0&RESPONSE-DATA-FORMAT=XML";
    foreach($_GET as $key => $value) {
        switch($key){
            case "keywords":
                $keywords = $value;
                $url .= "&keywords=".urlencode($value);
                break;
            case "paginationInput_entriesPerPage":
                $url .= "&paginationInput.entriesPerPage=".$value;
                break;
            case "sortOrder":
                $url .= "&sortOrder=".$value;
                break;
            case "MinPrice":
            case "MaxPrice":
                if($value != ""){
                    $url .= "&itemFilter[".$count."].name=".$key.
                            "&itemFilter[".$count."].value=".$value;
                    $count++;
                }
                break;
            case "Condition":
            case "ListingType":
                $url .= "&itemFilter[".$count."].name=".$key;
                $n = 0;
                foreach($value as $v){
                    $url .= "&itemFilter[".$count."].value[".$n."]=".$v;
                    $n++;
                }
                $count++;
                break;
            case "ReturnsAcceptedOnly":
            case "FreeShippingOnly":
            case "ExpeditedShippingType":
                $url .= "&itemFilter[".$count."].name=".$key.
                        "&itemFilter[".$count."].value=".$value;
                $count++;
                break;
            case "MaxHandlingTime":
                if($value != ""){
                    $url .= "&itemFilter[".
                            $count."].name=MaxHandlingTime&itemFilter[".
                            $count."].value=".$value;
                    $count++;
                }
                break;
            case "page":
                $url .= "&paginationInput.pageNumber=".$value;
        }
    }
    $url .= "&outputSelector[0]=SellerInfo&outputSelector[1]=PictureURLSuperSize&outputSelector[2]=StoreInfo"; 
    $xml = simplexml_load_file($url);
    $totalEntries = $xml->paginationOutput->totalEntries;
    if($totalEntries == 0){
        $result = array("ack" => "No results found");
        echo json_encode($result);
    }
    else{
        $result = array("ack" => (string)$xml->ack);
        $result += array("resultCount" => (string)$xml->paginationOutput->totalEntries);
        $result += array("pageNumber" => (string)$xml->paginationOutput->pageNumber);
        $num = $xml->paginationOutput->entriesPerPage;
        $result += array("itemCount" => (string)$num);
        $remainItems = (int)$totalEntries - (int)$num * ((int)$xml->paginationOutput->pageNumber - 1);
        if((int)$num > (int)$remainItems){
            $num = $remainItems;   
        }
        $xmlitems = $xml->searchResult->item;
        for($i = 0; $i < $num; ++$i){
            //create basicInfo
            $basicInfo = array("title" => (string)$xmlitems[$i]->title);
            $basicInfo += array("viewItemURL" => (string)$xmlitems[$i]->viewItemURL);
            $basicInfo += array("galleryURL" => (string)$xmlitems[$i]->galleryURL);
            $basicInfo += array("pictureURLSuperSize" => (string)$xmlitems[$i]->pictureURLSuperSize);
            $basicInfo += array("convertedCurrentPrice" => (string)$xmlitems[$i]->sellingStatus->convertedCurrentPrice);
            $basicInfo += array("shippingServiceCost" => (string)$xmlitems[$i]->shippingInfo->shippingServiceCost);
            $basicInfo += array("conditionDisplayName" => (string)$xmlitems[$i]->condition->conditionDisplayName);
            $basicInfo += array("listingType" => (string)$xmlitems[$i]->listingInfo->listingType);
            $basicInfo += array("location" => (string)$xmlitems[$i]->location);
            $basicInfo += array("categoryName" => (string)$xmlitems[$i]->primaryCategory->categoryName);
            $basicInfo += array("topRatedListing" => (string)$xmlitems[$i]->topRatedListing);
            //create sellerInfo
            $sellerInfo = array("sellerUserName" => (string)$xmlitems[$i]->sellerInfo->sellerUserName);
            $sellerInfo += array("feedbackScore" => (string)$xmlitems[$i]->sellerInfo->feedbackScore);
            $sellerInfo += array("positiveFeedbackPercent" => (string)$xmlitems[$i]->sellerInfo->positiveFeedbackPercent);
            $sellerInfo += array("feedbackRatingStar" => (string)$xmlitems[$i]->sellerInfo->feedbackRatingStar);
            $sellerInfo += array("topRatedSeller" => (string)$xmlitems[$i]->sellerInfo->topRatedSeller);
            $sellerInfo += array("sellerStoreName" => (string)$xmlitems[$i]->storeInfo->storeName);
            $sellerInfo += array("sellerStoreURL" => (string)$xmlitems[$i]->storeInfo->storeURL);
            //create shippingInfo
            $shippingInfo = array("shippingType" => (string)$xmlitems[$i]->shippingInfo->shippingType);
            $shipToLocations = $xmlitems[$i]->shippingInfo->shipToLocations;
            $len = count($shipToLocations);
            $locations = (string)$shipToLocations;
            if($len > 1){
                for($j = 1; $j < $len; ++$j){
                    $locations .= "," . $shipToLocations[$j];
                }
            }
            $shippingInfo += array("shipToLocations" => $locations);
            $shippingInfo += array("expeditedShipping" => (string)$xmlitems[$i]->shippingInfo->expeditedShipping);
            $shippingInfo += array("oneDayShippingAvailable" => (string)$xmlitems[$i]->shippingInfo->oneDayShippingAvailable);
            $shippingInfo += array("returnsAccepted" => (string)$xmlitems[$i]->returnsAccepted);
            $shippingInfo += array("handlingTime" => (string)$xmlitems[$i]->shippingInfo->handlingTime);
            $item = array("basicInfo" => $basicInfo);
            $item += array("sellerInfo" => $sellerInfo);
            $item += array("shippingInfo" => $shippingInfo);
            $itemName = "item" . $i;
            $result += array($itemName => $item);
        }
        echo json_encode($result);
    }
?>