//function to create REST client request
function init(url) {
	var req;
	if (window.XMLHttpRequest) {
		req = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		req = new ActiveXObject("Microsoft.XMLHTTP");
	}
	
	req.open("GET", url, true); //last parameter is true for asynchronous requests
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	
	return req;
}

function getLabelProductReport() {
	Ext.getCmp('centerGridPanel').setLoading();
	var fromValue = Ext.getCmp('from_hr').getValue();
	var toValue = Ext.getCmp('to_hr').getValue();
	var isTodayReport = true;
	if(Ext.getCmp('reportTypeCombo').getValue()=='Date Range'){
		isTodayReport = false;
		fromValue = Ext.util.Format.date(Ext.getCmp('from_date').getValue(),'d-m-Y');
		toValue = Ext.util.Format.date(Ext.getCmp('to_date').getValue(),'d-m-Y');
	}
	var req = init("/RESTESELogParser/logparser/label_product_failures_report/"+fromValue+"/"+toValue+"/"+isTodayReport);
	req.onreadystatechange = function(){loadLabelProductReport(req);}
	req.send();	
}

function loadLabelProductReport(req){
	if (req.readyState == 4) {				
		if (req.status == 200) {			
			Ext.getCmp('centerGridPanel').setLoading(false);
			eventsStore.removeAll();			
			var mydata = req.responseText;
			try{
				var events = eval('(' + mydata+ ')');
				eventsStore.loadData(events);
				Ext.getCmp('centerGridPanel').setTitle('Total failures:'+eventsStore.count());
				Ext.getCmp('viewConfig').refresh();
			} catch (e){
				Ext.getCmp('viewConfig').emptyText = '<center><b><br><br> Oops! some exception occured in reading the results.<br>Please contact admin.</b></center>'
				Ext.getCmp('viewConfig').refresh();
				console.log(e);
				console.log(e.message);
				console.log(e.description);
			}
		}
	}
}

function renderSelectedReport(reportName){
	if(reportName == 'Label Product Report'){
		getCurrentDateTime();		
	}else {
		Ext.getCmp('contentPanePanel').setTitle('ESE File Log Parser');
		Ext.getCmp('centerPanel').removeAll();
	}
}

function showErrorStack(errorStack){
	var labelReportErrorStackWindow = Ext.create('Ext.window.Window', {
	    title: 'Error Stack',
	    id : 'labelReportErrorStackWindow',
	    height: 300,
	    width: 590,
	    modal:true,
	    closable:false,	
        renderTo : Ext.get('contentPane'),
		layout : {
			type : 'vbox',
			pack : 'start',
			align : 'stretch'
		},					
		items : [{
	        xtype     : 'textareafield',
	        readOnly  : true,
	        height    : 238,
	        value	  : errorStack
	    }],
		fbar : ['->', {
			xtype : 'button',
			width : 40,
			text : 'Ok',
			handler : function(event, toolEl, panel) {
				labelReportErrorStackWindow.destroy();
			}
		}]
	}).show();
}

function isRequestValid(){
	if(Ext.getCmp('reportTypeCombo').getValue()=='Date Range' && Ext.getCmp('from_date').getValue().getTime() > Ext.getCmp('to_date').getValue().getTime()){
		alert("To date must be greater than or equal to From Date");
		return false;
	}
	if(Ext.getCmp('reportTypeCombo').getValue()=='Time Range' && Ext.getCmp('from_hr').getValue() > Ext.getCmp('to_hr').getValue()){
		alert("To Hr must be greater than or equal to From Hr");
		return false;
	}
	return true;
}

function getCurrentDateTime() {	
	var req = init("/RESTESELogParser/logparser/getcurrentdatetime");
	req.onreadystatechange = function(){loadCurrentDateTime(req);}
	req.send();	
}

function loadCurrentDateTime(req) {
	if (req.readyState == 4) {				
		if (req.status == 200) {
			currentDateTime = req.responseText;
			currentDate = Ext.Date.parse(currentDateTime.split(' ')[0], 'd-m-Y');
			currentHour = currentDateTime.split(' ')[1].split(':')[0];
			
			var centerSearchPanel = Ext.create('Ext.panel.Panel', {
			    height:60,
			    defaults : {
		            margins:'10 0 0 0'
		        },
			    layout : {
					type : 'hbox',
					pack : 'start',
					align : 'center'
				},
				title: '',		
				id:'centerSearchPanel',
				items: [{ 
					xtype: 'combo',
			        flex:2,
			        labelWidth:80,
			        id:'reportTypeCombo',
			        fieldLabel: 'Report Type',
			        store: ReportTypeStore,
			        queryMode: 'local',
			        displayField: 'reporType',
			        valueField: 'reporType',
			        labelAlign:'right',
			        value:'Time Range',
			        listeners:{
			            scope: this,
			           'select': function(){
			        	   if(Ext.getCmp('reportTypeCombo').getValue()=='Date Range'){
			        		   Ext.getCmp('from_hr').disable();
			        		   Ext.getCmp('to_hr').disable();
			        		   Ext.getCmp('from_date').enable();
			        		   Ext.getCmp('to_date').enable();
			        	   }else {
			        		   Ext.getCmp('from_hr').enable();
			        		   Ext.getCmp('to_hr').enable();
			        		   Ext.getCmp('from_date').disable();
			        		   Ext.getCmp('to_date').disable();
			        	   }
			           }
			       }
				},{ 
					xtype: 'datefield',	        
			        fieldLabel: 'From',
			        id: 'from_date',
			        format: 'd-m-Y',
			        flex:2,
			        disabled:true,
			        labelWidth:40,
			        labelAlign:'right',
			        value: new Date(currentDate-86400000),
			        maxValue: new Date(currentDate-86400000)
				},{ 
					xtype: 'datefield',
			        fieldLabel: 'To',
			        id: 'to_date',
			        disabled:true,
			        value: new Date(currentDate-86400000),
			        format: 'd-m-Y',
			        labelAlign:'right',
			        flex:2,
			        labelWidth:40,
			        maxValue: new Date(currentDate-86400000)
				},{
			        xtype: 'numberfield',
			        id: 'from_hr',
			        flex:2,
			        labelWidth:50,			        
			        labelAlign:'right',
			        fieldLabel: 'From Hr',
			        value: 0,
			        maxValue: currentHour,
			        minValue: 0
			    },{
			        xtype: 'numberfield',
			        id: 'to_hr',			        
			        flex:2,
			        labelWidth:50,
			        labelAlign:'right',
			        fieldLabel: 'To Hr',
			        value: currentHour,
			        maxValue: currentHour,
			        minValue: 0
			    },{ 
					xtype: 'button',
			        margins:'10 0 0 5',
			        width:40,
			        text: 'Go',
			        handler: function(){
			        	if(isRequestValid()){
			        		getLabelProductReport();
			        	}	        	
			        }
				}]
			});
			
			var centerGridPanel = Ext.create('Ext.grid.Panel', {
				title: 'Total failures:',
				margin: '0 0 0 0',
				store: eventsStore,		
				id:'centerGridPanel',
				viewConfig: {
		        	id: 'viewConfig',
		            emptyText: '<center><b><br><br>No matches found for the search criteria..!</center>' 
		        },
				columns: [	        
				    { header: 'Label', dataIndex: 'eventData', flex: 3,renderer:function(value){return value.VIEW_LABEL;}},		            
				    { header: 'Host Name', dataIndex: 'hostName', flex: 2},
				    { header: 'Agent Version', dataIndex: 'agentVersion', flex: 4},
				    {
						xtype:'actioncolumn',
						header: 'Error Stack',
						flex : 1,
						align : 'center',				
						items:[{
							icon :'/RESTESELogParser/resources/images/information.png',
							tooltip : 'open',
							handler: function(grid, rowIndex, colIndex) {
		                    	showErrorStack(grid.getStore().getAt(rowIndex).data.eventData.ERROR_STACK);                    	
		                	}					
						}]
					}
				],
				flex:1,
				layout: 'fit'
			});
			
			Ext.getCmp('contentPanePanel').setTitle('ESE File Log Parser : Label Product Report');
			Ext.getCmp('centerPanel').add(centerSearchPanel,centerGridPanel);
			getLabelProductReport();
		}
	}
}
