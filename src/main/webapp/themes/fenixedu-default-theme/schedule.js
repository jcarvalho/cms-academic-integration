function date(weekday, hour, minutes) {
	return moment().day( weekday - 1).hour( hour + 1 ).minute( minutes ).toISOString();
}

var scheduleEvents = [];
{% for lesson in schedule %}
	{% set over = "Semanas " + lesson.shiftWeeks %}
	scheduleEvents.push({
		id: '{{ lesson.id }}',
		title: '{{ lesson.shiftTypeCode }} \n({{ lesson.shiftSpace }})',
		start: '{{ lesson.start }}',
		end: '{{ lesson.end }}',
		color: '#{{ lesson.color }}',
		description: "<p>{{ lesson.shiftType }}</p><p>{{ i18n('resources.FenixEduCMSResources', 'label.weeks') }}</p><p>{{ lesson.shiftWeeks }}</p>",
		textColor: '{{ lesson.textColor }}'
	});
{% endfor %}

var i18nDayNames = [
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.sunday') }}',
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.monday') }}',
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.tuesday') }}',
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.wednesday') }}',
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.thursday') }}',
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.friday') }}',
	'{{ i18n('resources.FenixEduCMSResources', 'weekday.saturday') }}'
];

var config = {
	firstDay: 0,
	allDaySlot: false,
	weekends: false,
	editable: false,
	defaultDate: date(1, 0, 0),
	defaultView: 'agendaWeek',
	timeFormat: 'HH:mm',
	axisFormat: 'HH:mm',
	minTime : '{{ minHour }}',
	maxTime : '{{ maxHour }}',
	slotDuration: '00:30:00',
	slotEventOverlap: false,
	dayNames: i18nDayNames,
	dayNamesShort: i18nDayNames,
	header: { left:   '', center: '', right:  '' },
	columnFormat: { week: 'ddd' },
	eventAfterRender: afterLessonRender,
	events: scheduleEvents
};

$(document).ready(function() {
	$('#calendar').fullCalendar(config);
});

function afterLessonRender( event, jsEvent, view ) {
	jsEvent.attr('title', event.description);
	jsEvent.tooltip({ container: 'body', html: true });
}