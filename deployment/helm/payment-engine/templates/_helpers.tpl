{{- define "payment-engine.name" -}}
{{- .Chart.Name -}}
{{- end -}}

{{- define "payment-engine.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else if .Values.nameOverride }}
{{- printf "%s" .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- include "payment-engine.name" . | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "payment-engine.labels" -}}
app.kubernetes.io/name: {{ include "payment-engine.name" . }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "payment-engine.component-name" -}}
{{- $root := index . 0 -}}
{{- $component := index . 1 -}}
{{- $name := index . 2 -}}
{{- if $component.fullnameOverride -}}
{{- $component.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else if $component.nameOverride -}}
{{- $component.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" (include "payment-engine.fullname" $root) $name | replace "_" "-" | lower | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
