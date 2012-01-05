from django.core.exceptions import ValidationError

def validate_percent(value):
    'Validiert Prozentwerte'
    if value < 0 or value > 100:
        raise ValidationError(u'%d is not a percent value' % value)

def validate_nonzeropositive(value):
    'Validiert einen Integer, der > 0 sein muss'
    if value < 1:
            raise ValidationError(u'%d must be greater 0' % value)