from django.core.exceptions import ValidationError

def validate_percent(value):
        'Validiert Prozentwerte'
        if value < 0 or value > 100:
            raise ValidationError(u'%d is not a percent value' % value)
