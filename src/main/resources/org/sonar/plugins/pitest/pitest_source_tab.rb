<link type="text/css" rel="stylesheet" media="all" href="<%= url_for_static(:plugin => 'pitest', :path => 'pitest.css') -%>">

<div class="gwt-ViewerHeader">
<table cellspacing="0" cellpadding="0">
  <tbody>
	<tr>
	  <td align="left" style="vertical-align: top; "><div class="big"><%= format_measure(measure('pitest_mutations_coverage')) %></div></td>

	  <%
	  ["pitest_mutations_total", "pitest_mutations_detected", "pitest_mutations_noCoverage", "pitest_mutations_killed",
	  "pitest_mutations_survived", "pitest_mutations_memoryError", "pitest_mutations_timedOut", "pitest_mutations_unknown"].each do |metric_name|
	  %>

	  <td align="left" style="vertical-align: top; "><div class="metric"><b><%= metric(metric_name).short_name%></b>: </div></td>
	  <td align="left" style="vertical-align: top; "><div class="value"><%= format_measure(measure(metric_name)) %></div></td>
	  <% end %>
	</tr>
  </tbody>
</table>
</div>
<table id="sourcePanel" class="gwt-SourcePanel" cellpadding="0" cellspacing="0" >
<tbody><tr><td align="left" style="vertical-align: top; ">
<table class="sources code">
<tbody>
  <%


  def getMutantStyle(mutant)
    styleStatusMap = {
    "KILLED" => "mutationkilled",
	"MEMORY_ERROR" => "mutationkilled",
	"TIMED_OUT" => "mutationkilled",
	"NO_COVERAGE" => "mutationnocoverage",
	"UNKNOWN" => "mutationnocoverage",
	"SURVIVED" => "mutationsurvived"
  }
    return styleStatusMap[mutant["s"]]
  end

  def countMutantsByStatus(mutants, *status)
	result = 0;
	mutants.each do |mutant|
	  if status.include?(mutant["s"])
	    result = result +1
	  end
	end
	return result
  end


  data = JSON.parse measure('pitest_mutations_data').data

  source_lines=@snapshot.source.syntax_highlighted_lines()

  source_lines.each_with_index do |line, index|
    mutants = data[(index+1).to_s]
    survivedMutants = 0;
    notCoveredMutants = 0;
    style = "";
	displayedIndex = (index+1).to_s
    unless mutants.nil?
	  survivedMutants = countMutantsByStatus(mutants, "SURVIVED")
	  notCoveredMutants = countMutantsByStatus(mutants, "NO_COVERAGE", "UNKNOWN")
	  if survivedMutants > 0
	    style = "red"
      elsif notCoveredMutants > 0
	    style = "orange"
	  else
	    style = "green"
	  end
    end
%>
  <tr id="pos<%= index+1 %>">
    <td id="L<%= displayedIndex %>">
	  <div class="ln <%= mutants.nil? ? "" : "mutationlidsection" %>"><%= displayedIndex %></div>
    </td>
    <td><div class="val <%= style %>"><%= mutants.nil? ? "&nbsp;" : mutants.length.to_s %></div></td>
    <td><div class="val <%= style %>"><%= mutants.nil? ? "&nbsp;" : survivedMutants.to_s + "/" + notCoveredMutants.to_s %></div></td>
    <td>
	  <div class="src <%= style %>"><pre><%= line%></pre></div>
	</td>
  </tr>
  <% unless mutants.nil? %>
  <tr>
    <td></td>
	<td></td>
	<td></td>
    <td>
      <div class="mutations">
      <% mutants.each do |mutant| %>
        <div class="mutation">
          <div class="vtitle">
            <span class="<%= getMutantStyle(mutant) %>"><%= mutant["s"] %></span>
            &nbsp;<img alt="Sep12" src="/images/sep12.png">&nbsp;
            <span class="mutationname"><%= mutant["mname"] %></span>
          </div>
          <div class="mutationDescription"><%= mutant["mdesc"] %></div>
        </div>
		&nbsp;
      <% end %>
      </div>
    </td>
  </tr>
  <% end %>
<% end %>
</tbody>
</table>
</td></tr>
</tbody>
</table>