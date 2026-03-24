<#ftl strip_whitespace=true>
<#list users as user><#--
User -->"${user.accessCode}";"${user.lastName}";"${user.firstName}";"${user.email}";"${user.status}";"${user.passwordMaxValidDate}";"${user.accountMaxValidDate}"<#--
Rôles --><#if user.roles??><#list user.roles as role>;"role:${role}"</#list></#if><#--
Groups --><#if user.groups??><#list user.groups as group>;"group:${group}"</#list></#if><#--
Attributs --><#if user.attributes??><#list user.attributes as attr>;"${attr.id}:${attr.fieldId}:${attr.value}"</#list></#if>
</#list>