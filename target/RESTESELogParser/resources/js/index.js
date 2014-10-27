Ext.onReady(function () {
	Ext.QuickTips.init();
	
	var contentPanePanel = Ext.create('Ext.panel.Panel', {
		height: "100%",
		width: "100%",
	    title: 'ESE File Log Parser',
	    layout: 'border',
	    id: 'contentPanePanel',
	    items: [{	        
	        title: 'Select Report',
	        region:'west',
	        xtype: 'gridpanel',
	        margins: '0 0 0 0',
	        width: 180,
	        collapsible: true,
	        id: 'westGridPanel',
	        layout: 'fit',
	        store: reportsListStore,
			columns: [
			    { header: 'Name', dataIndex: 'reportName', flex:1}
			],			
		    listeners: {
		        itemclick: function(dataview, record, item, index, e) {
		            renderSelectedReport(record.data.reportName);
		        }
		    }
			
	    },{
	    	xtype:'panel',
	    	id:'centerPanel',	    		    		    	
	    	items:[],
	    	layout : {
				type : 'vbox',
				pack : 'start',
				align : 'stretch'
			},
			region:'center'
	    }],
	    renderTo: Ext.get('contentPane')
	});
});