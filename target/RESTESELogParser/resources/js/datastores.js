eventsStore = Ext.create('Ext.data.Store', {
	model: 'eventsModel',
	autoLoad: false	
});

reportsListStore = Ext.create('Ext.data.Store', {
    model: 'ReportsListModel',
    data : [
        {reportName: 'Label Product Report',desc:''},        
        {reportName: 'Push Failures Report',desc:''}
    ]
});

ReportTypeStore = Ext.create('Ext.data.Store', {
    fields: ['reporType'],
    data : [
        {"reporType":"Date Range"},
        {"reporType":"Time Range"}
    ]
});