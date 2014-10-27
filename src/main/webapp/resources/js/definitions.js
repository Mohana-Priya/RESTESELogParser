Ext.define('eventsModel', {
    extend: 'Ext.data.Model',
    fields: [
		{name:'time'},	
		{name:'agentName'},
		{name:'agentVersion'},
		{name:'type'},
		{name:'hostName'},
		{name:'ipAddress'},
		{name:'userName'},	
		{name:'osName'},
		{name:'osVersion'},
		{name:'osArchitecture'},
		{name:'eventData'}
    ]
});

Ext.define('ReportsListModel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'reportName'},
        {name: 'desc'}
    ]
});