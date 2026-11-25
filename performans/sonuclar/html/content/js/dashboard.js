/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 99.66648137854364, "KoPercent": 0.33351862145636463};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.8344913841022791, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.5, 500, 1500, "Hesap aç (S1 yatırma hesabı)"], "isController": false}, {"data": [1.0, 500, 1500, "Hesap aç (S2 transfer A)"], "isController": false}, {"data": [0.0, 500, 1500, "KONTROL S1: son bakiye = başarılı yatırma sayısı × 1 TL"], "isController": false}, {"data": [1.0, 500, 1500, "Hesap aç (S2 transfer B)"], "isController": false}, {"data": [1.0, 500, 1500, "KONTROL S3: açılan hesap numaraları benzersiz mi (R-1.5)"], "isController": false}, {"data": [0.8407329105003524, 500, 1500, "S1 bakiye sorgula"], "isController": false}, {"data": [0.8438147892190739, 500, 1500, "S1 para yatır (1 TL)"], "isController": false}, {"data": [0.7846441947565543, 500, 1500, "S2 transfer A→B (10 TL)"], "isController": false}, {"data": [0.875, 500, 1500, "S3 hesap aç"], "isController": false}, {"data": [1.0, 500, 1500, "KONTROL S2b: A + B toplamı 20.000 TL olmalı (G-5)"], "isController": false}, {"data": [0.7693798449612403, 500, 1500, "S2 transfer B→A (10 TL)"], "isController": false}, {"data": [1.0, 500, 1500, "KONTROL S2a: A hesabı bakiyesi"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 3598, 12, 0.33351862145636463, 799.3132295719862, 99, 10347, 290.0, 1309.0, 3294.0, 7320.0, 30.25665175417942, 25.918177011503918, 6.405683686383666], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["Hesap aç (S1 yatırma hesabı)", 1, 0, 0.0, 777.0, 777, 777, 777.0, 777.0, 777.0, 777.0, 1.287001287001287, 1.1437218468468469, 0.3305481821106821], "isController": false}, {"data": ["Hesap aç (S2 transfer A)", 1, 0, 0.0, 285.0, 285, 285, 285.0, 285.0, 285.0, 285.0, 3.5087719298245617, 3.0838815789473686, 0.9046052631578948], "isController": false}, {"data": ["KONTROL S1: son bakiye = başarılı yatırma sayısı × 1 TL", 1, 1, 100.0, 282.0, 282, 282, 282.0, 282.0, 282.0, 282.0, 3.5460992907801416, 3.0959109042553195, 0.6337267287234043], "isController": false}, {"data": ["Hesap aç (S2 transfer B)", 1, 0, 0.0, 315.0, 315, 315, 315.0, 315.0, 315.0, 315.0, 3.1746031746031744, 2.787078373015873, 0.8184523809523809], "isController": false}, {"data": ["KONTROL S3: açılan hesap numaraları benzersiz mi (R-1.5)", 1, 0, 0.0, 99.0, 99, 99, 99.0, 99.0, 99.0, 99.0, 10.101010101010102, 0.4537563131313131, 0.0], "isController": false}, {"data": ["S1 bakiye sorgula", 1419, 0, 0.0, 785.5313601127559, 238, 10347, 293.0, 1309.0, 3293.0, 7319.2, 21.895445006789284, 19.047775617014874, 3.9129555041430066], "isController": false}, {"data": ["S1 para yatır (1 TL)", 1447, 0, 0.0, 772.5694540428472, 242, 10314, 292.0, 1308.0, 3293.2, 7320.16, 21.23195210705482, 18.531660502443067, 4.644489523418241], "isController": false}, {"data": ["S2 transfer A→B (10 TL)", 267, 3, 1.1235955056179776, 1005.9550561797757, 241, 10344, 283.0, 3277.2, 3323.6, 10308.56, 8.222468588322247, 6.315266566195492, 2.176063464292313], "isController": false}, {"data": ["S3 hesap aç", 200, 0, 0.0, 608.1399999999999, 239, 7289, 279.0, 1294.6, 3273.9, 3313.99, 15.631105900742476, 13.728278258108636, 4.014629738178976], "isController": false}, {"data": ["KONTROL S2b: A + B toplamı 20.000 TL olmalı (G-5)", 1, 0, 0.0, 370.0, 370, 370, 370.0, 370.0, 370.0, 370.0, 2.7027027027027026, 2.3384712837837838, 0.48300253378378377], "isController": false}, {"data": ["S2 transfer B→A (10 TL)", 258, 8, 3.10077519379845, 971.7325581395347, 246, 10283, 282.5, 3254.1, 3344.7499999999995, 7315.280000000001, 7.472629322829172, 5.753589686033713, 1.9776196743034233], "isController": false}, {"data": ["KONTROL S2a: A hesabı bakiyesi", 1, 0, 0.0, 299.0, 299, 299, 299.0, 299.0, 299.0, 299.0, 3.3444816053511706, 2.903558737458194, 0.5976954431438127], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["500/Internal Server Error", 11, 91.66666666666667, 0.3057254030016676], "isController": false}, {"data": ["TUTARSIZ (lost update?): S1 son bakiye=1324.0 TL, başarılı yatırma=1447 (beklenen bakiye=1447 TL)", 1, 8.333333333333334, 0.027793218454697052], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 3598, 12, "500/Internal Server Error", 11, "TUTARSIZ (lost update?): S1 son bakiye=1324.0 TL, başarılı yatırma=1447 (beklenen bakiye=1447 TL)", 1, "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["KONTROL S1: son bakiye = başarılı yatırma sayısı × 1 TL", 1, 1, "TUTARSIZ (lost update?): S1 son bakiye=1324.0 TL, başarılı yatırma=1447 (beklenen bakiye=1447 TL)", 1, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["S2 transfer A→B (10 TL)", 267, 3, "500/Internal Server Error", 3, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": ["S2 transfer B→A (10 TL)", 258, 8, "500/Internal Server Error", 8, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
