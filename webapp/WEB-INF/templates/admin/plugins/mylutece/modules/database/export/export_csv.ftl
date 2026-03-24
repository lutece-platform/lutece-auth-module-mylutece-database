<#function escape val>
    <#local s = val?string>
    <#if s?contains(";") || s?contains('"') || s?contains("\n")>
        <#return '"' + s?replace('"', '""') + '"'>
    </#if>
    <#return s>
</#function><#--
User --><#list users as user><#--
-->"${escape(user.accessCode!)}"<#--
-->;"${escape(user.lastName!)}"<#--
-->;"${escape(user.firstName!)}"<#--
-->;"${escape(user.email!)}"<#--
-->;"${escape(user.status!)}"<#--
-->;"${escape(user.passwordMaxValidDate!)}"<#--
-->;"${escape(user.accountMaxValidDate!)}"<#--
Rôles --><#if user.roles??><#list user.roles as role>;"role:${escape(role!)}"</#list></#if><#--
Groups --><#if user.groups??><#list user.groups as group>;"group:${escape(group!)}"</#list></#if><#--
Attributes --><#if user.attributes??><#list user.attributes as attr>;"${escape(attr!)}"</#list></#if>
</#list>