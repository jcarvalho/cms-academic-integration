var h = [];

function selectGrouping(groupingId) {
	execute(function(){
		$('#groupings').hide();
		$('#shifts').show();
		$('#students').show();
		
		$('[grouping]').hide();
		$('[grouping='+ groupingId +']').show();
	});
}

function selectShift(shiftId) {
	execute(function() {
		$('#groupings').fadeOut();
		$('#shifts').fadeIn();
		$('#students').fadeIn();
		
		$('[student-group]').fadeIn();
		if(shiftId) {
			$('[shift]').fadeOut();
			$('[shift='+ shiftId +']').fadeIn();
		} else {
			$('[shift]').fadeIn();
		}
	});
}

function selectStudentGroup(groupId) {
	if ( $("#shifts").is(":visible") ) {
	    studentsGroup(groupId);
	} else { 
		studentsOnly(groupId);
	}
}

function studentsGroup(groupId) {
	execute(function() {
		$('#groupings').fadeOut();
		$('#shifts').fadeIn();
		$('#students').fadeIn();
		if(groupId) {
			$('[student-group]').fadeOut();
			$('[student-group=' + groupId + ']').fadeIn();
		} else {
			$('[student-group]').fadeIn();
		}
	});
}

function studentsOnly(groupId) {
	execute(function() {
		$('#groupings').fadeOut();
		$('#shifts').fadeOut();
		$('#students').fadeIn();
		if(groupId) {
			$('[student-group]').fadeOut();
			$('[student-group=' + groupId + ']').fadeIn();
		} else {
			$('[student-group]').fadeIn();
		}
	});
}

function restart() {
	h = [];
	$('section').hide();
	$('#groupings').show();
}

function execute(fn) {
	fn();
	console.log("before",h);
	h.push(fn);
	console.log("after:",h);
}

function back() {
	console.log("before",h);
	if(h.length > 1) {
		h.pop();
		var method = h.pop();
		method();
	} else {
		console.log("restart",h);
		restart();
	}
	console.log("after",h);
}