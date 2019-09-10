sigma = [0.5 1 2];
for n = 1:size(sigma,2)
    image = imread(strcat(num2str(sigma(n)),'_gaussian.jpg'));
    %take local differences
    diff_x = conv2(image,[1/2,0,-1/2],'same');
    figure;
    %draw the normalized histogram
    [value edge] = histcounts(diff_x,'Normalization','pdf');
    %set the yscale to be logged
    value = log(value)
    bar_center = 0.5*(edge(1:end-1) + edge(2:end));
    h = bar(bar_center,value);
    %save pictures
    saveas(h,strcat(num2str(sigma(n)),'_histogram.jpg'))
end