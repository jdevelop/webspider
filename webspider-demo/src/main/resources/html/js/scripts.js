// handle the URL submission
$(function () {
    $('#start_validation').submit(function (event) {
        $.post('/validate',
            $('#start_validation').serialize(),
            function (data) {
                alert(data);
            }
        );
        event.preventDefault();
    });
});