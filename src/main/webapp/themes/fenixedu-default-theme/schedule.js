function date(weekday, hour, minutes) {
	return moment().day( weekday - 1).hour( hour + 1 ).minute( minutes ).toISOString();
}

var scheduleEvents = [];
{% for lesson in schedule %}
	{% set over = "Semanas " + lesson.shiftWeeks %}
	scheduleEvents.push({
		id: '{{ lesson.id }}',
		title: '{{ lesson.shiftTypeCode }} \n({{ lesson.shiftSpace }})',
		start: date({{ lesson.weekDay }}, {{ lesson.beginHour }}, {{ lesson.beginMinutes }}),
		end: date({{ lesson.weekDay }}, {{ lesson.endHour }}, {{ lesson.endMinutes }}),
		url: '{{ lesson.spaceUrl }}',
		color: '{{ lesson.color }}',
		description: '<p>{{ lesson.shiftType }}</p><p>Semanas \n {{ lesson.shiftWeeks }}</p>',
		textColor: '#333'
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

$(document).ready(function() {
	$('#calendar').fullCalendar({
		firstDay: 0,
		allDaySlot: false,
		weekends: false,
		editable: false,
		defaultDate: date(1, 0, 0),
		defaultView: 'agendaWeek',
		timeFormat: 'HH:mm',
		axisFormat: 'HH:mm',
		minTime : '{{ minHour - 1 }}:00:00',
		maxTime : '{{ maxHour + 1 }}:00:00',
		slotDuration: '00:30:00',
		slotEventOverlap: false,
		dayNames: i18nDayNames,
		dayNamesShort: i18nDayNames,
		header: { left:   '', center: '', right:  '' },
		columnFormat: { week: 'ddd' },
		eventAfterRender: afterLessonRender,
		events: scheduleEvents
	});

});

function afterLessonRender( event, jsEvent, view ) {
	jsEvent.attr('title', event.description);
	jsEvent.tooltip({ container: 'body', html: true });
}